package com.github.laxika.magicalvibes.cards.h;

<<<<<<< HEAD
import com.github.laxika.magicalvibes.cards.c.Clone;
=======
import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
>>>>>>> 3329cc0590 (Review INR 279 tests)
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

<<<<<<< HEAD
@CardUsed({HanweirBattlements.class, HanweirGarrison.class, HanweirTheWrithingTownship.class, Clone.class})
=======
@CardUsed({HanweirBattlements.class, HanweirGarrison.class, HanweirTheWrithingTownship.class,
        CacklingCounterpart.class})
>>>>>>> 3329cc0590 (Review INR 279 tests)
class HanweirBattlementsTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T} grants haste to a target creature")
    void grantsHaste() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, garrison.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, garrison, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Meld ability exiles both halves and melds into Hanweir, the Writhing Township")
    void meldsWithGarrison() {
        Permanent battlements = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(battlements.getId()) || p.getId().equals(garrison.getId()));
        Permanent melded = findPermanent(player1, "Hanweir, the Writhing Township");
        assertThat(melded.getCard()).isInstanceOf(HanweirTheWrithingTownship.class);
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Meld ability does nothing without an owned Hanweir Garrison")
    void doesNothingWithoutPartner() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        harness.addToBattlefield(player2, new HanweirGarrison());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
    }

    @Test
<<<<<<< HEAD
    @DisplayName("Tapping Battlements adds colorless mana without using the stack")
    void addsColorlessMana() {
        Permanent battlements = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());

        harness.tapPermanent(player1, 0);

        assertThat(battlements.isTapped()).isTrue();
=======
    void tapProducesColorlessManaWithoutUsingTheStack() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
>>>>>>> 3329cc0590 (Review INR 279 tests)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
<<<<<<< HEAD
    @DisplayName("Meld condition is checked again when Garrison leaves in response")
    void partnerLeavingInResponsePreventsMeld() {
=======
    void canGrantHasteToAnOpponentsCreature() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player2, new HanweirGarrison());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, garrison.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, garrison, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotMeldWithAPartnerOwnedByTheOpponent() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        gd.stolenCreatures.put(garrison.getId(), player2.getId());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Battlements");
        harness.assertOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
    }

    @Test
    void doesNotMeldWhenBattlementsIsOwnedByTheOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        harness.addToBattlefield(player1, new HanweirGarrison());
        gd.stolenCreatures.put(land.getId(), player2.getId());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Battlements");
        harness.assertOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
    }

    @Test
    void doesNothingWhenPartnerLeavesBeforeResolution() {
>>>>>>> 3329cc0590 (Review INR 279 tests)
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        addMeldMana();
        harness.activateAbility(player1, 0, 2, null, null);
<<<<<<< HEAD
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, garrison));
=======
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, garrison));
>>>>>>> 3329cc0590 (Review INR 279 tests)

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
<<<<<<< HEAD
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Hanweir Garrison");
    }
    @Test
    @DisplayName("Controller chooses which Garrison melds when multiple are eligible")
    void choosesPartnerAtResolution() {
=======
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void tokenCopyCannotMeldAndBattlementsRemainsInExile() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, garrison.getId());
        assertThat(findPermanents(player1, "Hanweir Garrison")).hasSize(2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, garrison));
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof HanweirBattlements);
    }

    @Test
    void controllerChoosesWhichGarrisonToMeld() {
>>>>>>> 3329cc0590 (Review INR 279 tests)
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        addMeldMana();
<<<<<<< HEAD
        harness.activateAbility(player1, 0, 2, null, null);

=======

        harness.activateAbility(player1, 0, 2, null, null);
>>>>>>> 3329cc0590 (Review INR 279 tests)
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, second.getId());
<<<<<<< HEAD
        Permanent melded = findPermanent(player1, "Hanweir, the Writhing Township");
        assertThat(melded.getMeldComponentCards()).contains(second.getOriginalCard())
                .doesNotContain(first.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
=======

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(findPermanent(player1, "Hanweir, the Writhing Township").getMeldComponentCards())
                .contains(second.getOriginalCard()).doesNotContain(first.getOriginalCard());
    }

    @Test
    void meldedTownshipCanAttackImmediatelyAndCreatesTappedAttackingHorrors() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        harness.addToBattlefield(player1, new HanweirGarrison());
        addMeldMana();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent township = findPermanent(player1, "Hanweir, the Writhing Township");
        assertThat(township.isTapped()).isFalse();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Eldrazi Horror");
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
        });
    }

    @Test
    void grantedHasteExpiresAfterTheTurn() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, garrison.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, garrison, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, garrison, Keyword.HASTE)).isFalse();
>>>>>>> 3329cc0590 (Review INR 279 tests)
    }
    private void addMeldMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("A token copy of Garrison is exiled but cannot meld")
    void tokenGarrisonCannotMeld() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        HanweirGarrison token = new HanweirGarrison();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
    }

    @Test
    @DisplayName("A copy of Garrison is exiled but cannot meld")
    void copyCannotMeld() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new HanweirGarrison());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Hanweir Battlements", "Clone");
        harness.assertOnBattlefield(player2, "Hanweir Garrison");
    }
}
