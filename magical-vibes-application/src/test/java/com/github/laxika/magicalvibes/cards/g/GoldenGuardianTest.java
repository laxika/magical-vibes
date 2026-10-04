package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.m.MistCloakedHerald;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldenGuardian.class, ColossalDreadmaw.class, ExpelFromOrazca.class,
        MistCloakedHerald.class, Naturalize.class})
class GoldenGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Returns transformed after fighting and dying during the activated ability")
    void returnsTransformedWhenItDiesAfterFight() {
        Permanent guardian = addGuardianReady(player1);
        Permanent dreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, dreadmaw.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent garrison = findPermanent(player1, "Gold-Forge Garrison");
        assertThat(garrison.isTransformed()).isTrue();
        assertThat(garrison.getOriginalCard()).isSameAs(guardian.getOriginalCard());
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertNotInGraveyard(player1, "Golden Guardian");
    }

    @Test
    @DisplayName("Rejects a target not controlled by the activating player")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent guardian = addGuardianReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
        assertThat(guardian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Back face adds two mana of the chosen color")
    void backFaceAddsTwoMana() {
        Permanent garrison = addGarrisonReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(garrison.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Back face creates a 4/4 colorless artifact Golem token")
    void backFaceCreatesGolemToken() {
        addGarrisonReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getName().equals("Golem")
                        && gqs.getEffectivePower(gd, permanent) == 4
                        && gqs.getEffectiveToughness(gd, permanent) == 4);
    }

    @Test
    @DisplayName("Cannot fight itself")
    void rejectsItselfAsTarget() {
        Permanent guardian = addGuardianReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, guardian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("Returns transformed after surviving the fight and dying later that turn")
    void returnsAfterLaterDestruction() {
        Permanent guardian = addGuardianReady(player1);
        Permanent herald = addCreatureReady(player1, new MistCloakedHerald());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, herald.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Golden Guardian");
        harness.assertInGraveyard(player1, "Mist-Cloaked Herald");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Gold-Forge Garrison").isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Golden Guardian");
    }

    @Test
    @DisplayName("Does not fight or return if destroyed before its ability resolves")
    void destructionBeforeResolutionDoesNotReturn() {
        Permanent guardian = addGuardianReady(player1);
        Permanent dreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, dreadmaw.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Golden Guardian");
        harness.assertNotOnBattlefield(player1, "Gold-Forge Garrison");
        assertThat(dreadmaw.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An illegal fight target prevents the delayed return from being established")
    void illegalTargetPreventsReturn() {
        Permanent guardian = addGuardianReady(player1);
        Permanent dreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, dreadmaw.getId());

        harness.setHand(player1, List.of(new ExpelFromOrazca(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dreadmaw.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Golden Guardian");
        harness.assertNotOnBattlefield(player1, "Gold-Forge Garrison");
    }

    @Test
    @DisplayName("Bouncing and recasting the Guardian breaks its delayed death trigger")
    void delayedReturnDoesNotFollowRecastGuardian() {
        GoldenGuardian card = new GoldenGuardian();
        Permanent guardian = addCreatureReady(player1, card);
        Permanent herald = addCreatureReady(player1, new MistCloakedHerald());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, herald.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recast = findPermanent(player1, "Golden Guardian");
        assertThat(recast.getId()).isNotEqualTo(guardian.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, recast.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Golden Guardian");
        harness.assertNotOnBattlefield(player1, "Gold-Forge Garrison");
    }

    @Test
    @DisplayName("The return trigger cannot return a card that left and reentered the graveyard")
    void delayedReturnDoesNotFollowNewGraveyardObject() {
        GoldenGuardian card = new GoldenGuardian();
        addCreatureReady(player1, card);
        Permanent dreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, dreadmaw.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Golden Guardian");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, card.getId()));
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Golden Guardian");
        harness.assertNotOnBattlefield(player1, "Gold-Forge Garrison");
    }

    @Test
    @DisplayName("The delayed return expires when the turn ends")
    void delayedReturnExpiresAfterTurn() {
        Permanent guardian = addGuardianReady(player1);
        Permanent herald = addCreatureReady(player1, new MistCloakedHerald());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, herald.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, guardian.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Golden Guardian");
        harness.assertNotOnBattlefield(player1, "Gold-Forge Garrison");
    }

    private Permanent addGuardianReady(Player player) {
        return addCreatureReady(player, new GoldenGuardian());
    }

    private Permanent addGarrisonReady(Player player) {
        GoldenGuardian card = new GoldenGuardian();
        Permanent garrison = addCreatureReady(player, card);
        garrison.setCard(card.getBackFaceCard());
        garrison.setTransformed(true);
        return garrison;
    }
}
