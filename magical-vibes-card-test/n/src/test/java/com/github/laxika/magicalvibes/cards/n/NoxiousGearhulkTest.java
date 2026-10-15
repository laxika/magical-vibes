package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousGearhulk.class, EagerConstruct.class})
class NoxiousGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may ability destroys another creature and gains life equal to its toughness")
    void destroysAnotherCreatureAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());

        castAndAcceptMay(target.getId());

        harness.assertNotOnBattlefield(player2, "Eager Construct");
        harness.assertInGraveyard(player2, "Eager Construct");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Declining the ETB may ability leaves the creature and life total unchanged")
    void decliningMayLeavesTargetAndLifeUnchanged() {
        harness.setLife(player1, 20);
        addCreatureReady(player2, new EagerConstruct());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player2.getId()).getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Eager Construct");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does not target Noxious Gearhulk itself")
    void doesNotTargetItself() {
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Noxious Gearhulk");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB does not gain life when the target is indestructible")
    void indestructibleTargetDoesNotGrantLife() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Eager Construct");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB can destroy your own other creature and gives life to the ability controller")
    void destroysOwnCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player1, new EagerConstruct());

        castAndAcceptMay(target.getId());

        harness.assertNotOnBattlefield(player1, "Eager Construct");
        harness.assertInGraveyard(player1, "Eager Construct");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain uses modified toughness as the destroyed creature last existed")
    void gainsLifeFromModifiedToughness() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());
        target.setToughnessModifier(3);
        target.setMarkedDamage(1);

        castAndAcceptMay(target.getId());

        harness.assertInGraveyard(player2, "Eager Construct");
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("A regenerated target survives and grants no life")
    void regeneratedTargetDoesNotGrantLife() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());
        target.setRegenerationShield(1);

        castAndAcceptMay(target.getId());

        harness.assertOnBattlefield(player2, "Eager Construct");
        harness.assertNotInGraveyard(player2, "Eager Construct");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The trigger still destroys its target after Gearhulk leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = findPermanent(player1, "Noxious Gearhulk");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, source));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Noxious Gearhulk");
        harness.assertInGraveyard(player2, "Eager Construct");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Another Noxious Gearhulk is a legal target")
    void canDestroyAnotherGearhulk() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new NoxiousGearhulk());

        castAndAcceptMay(target.getId());

        harness.assertNotOnBattlefield(player2, "Noxious Gearhulk");
        harness.assertInGraveyard(player2, "Noxious Gearhulk");
        harness.assertOnBattlefield(player1, "Noxious Gearhulk");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A target that leaves before resolution grants no life")
    void absentTargetDoesNotGrantLife() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player2, new EagerConstruct());
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, target));

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }

    private void castAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NoxiousGearhulk(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }
}
