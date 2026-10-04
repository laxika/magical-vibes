package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.a.AkromaAngelOfFury;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FracturedIdentity.class, GrizzlyBears.class, Forest.class, AkromaAngelOfFury.class,
        LlanowarElves.class, Rancor.class, SolRing.class, Clone.class, ElvishVisionary.class})
class FracturedIdentityTest extends BaseCardTest {

    @Test
    void exilesTargetAndCreatesCopyForEachPlayerOtherThanItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFracturedIdentity(target, player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void doesNotTargetLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetingOwnPermanentGivesOpponentACopyWithItsManaAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        castFracturedIdentity(target, player1);

        harness.assertNotOnBattlefield(player1, "Sol Ring");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void copyingExistingTokenRetainsItsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        castFracturedIdentity(target, player1);
        Permanent firstToken = gd.playerBattlefields.get(player1.getId()).getFirst();

        castFracturedIdentity(firstToken, player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCard().isToken()).isTrue();
        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void copiesDoNotInheritCountersOrTappedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        target.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();

        castFracturedIdentity(target, player1);

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCounters()).isEmpty();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void auraCopyEntersAttachedToRecipientsChosenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Rancor());
        aura.setAttachedTo(creature.getId());

        castFracturedIdentity(aura, player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.assertNotOnBattlefield(player2, "Rancor");
        assertThat(gd.findExiledCard(aura.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token ->
                        assertThat(token.getAttachedTo()).isEqualTo(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    void copyingFaceDownPermanentUsesItsFaceDownCharacteristics() {
        harness.setHand(player1, List.of(new AkromaAngelOfFury()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.isFaceDown()).isTrue();

        castFracturedIdentity(target, player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isFalse();
        assertThat(token.getCard().getActivatedAbilities()).isEmpty();
    }

    @Test
    void tokenCopyTriggersItsEnterAbilityForItsRecipient() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishVisionary());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());

        castFracturedIdentity(target, player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void copyingACopyUsesTheCopiedCreatureRatherThanTheOriginalCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).getFirst();

        castFracturedIdentity(clone, player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Llanowar Elves");
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
        assertThat(gd.findExiledCard(clone.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void recipientsUseControllerAtResolutionRatherThanOwnerOrControllerAtCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(player1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(gd.findExiledCard(target.getCard().getId()).ownerId()).isEqualTo(player2.getId());
    }

    @Test
    void targetLeavingBeforeResolutionPreventsTokenCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(player1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }

    private void castFracturedIdentity(Permanent target, Player caster) {
        harness.setHand(caster, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(caster);
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addFracturedIdentityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
