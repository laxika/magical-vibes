package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BoonSatyr;
import com.github.laxika.magicalvibes.cards.g.GaeasEmbrace;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YennaRedtoothRegent.class, GloriousAnthem.class, GaeasEmbrace.class, GrizzlyBears.class,
        BoonSatyr.class, NyleaGodOfTheHunt.class})
class YennaRedtoothRegentTest extends BaseCardTest {

    @Test
    void copiesAnEligibleEnchantmentWithoutUntappingOrScry() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        activate(yenna, anthem);

        assertThat(findPermanents(player1, "Glorious Anthem")).hasSize(2);
        assertThat(findPermanents(player1, "Glorious Anthem")).filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(yenna.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetAnEnchantmentWithTheSameNameYouControl() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("doesn't have the same name");
    }

    @Test
    void ignoresAnOpponentsPermanentWithTheSameName() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());

        activate(yenna, anthem);

        assertThat(findPermanents(player1, "Glorious Anthem")).filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void copiesAnAuraThenUntapsYennaAndScriesTwo() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GaeasEmbrace());
        aura.setAttachedTo(creature.getId());

        activate(yenna, aura);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, yenna.getId());

        assertThat(findPermanents(player1, "Gaea's Embrace")).filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> assertThat(token.getAttachedTo()).isEqualTo(yenna.getId()));
        assertThat(yenna.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(yenna.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetAnOpponentsEnchantment() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetANonenchantmentCreature() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        addManaForAbility();
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, yenna.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateDuringAnOpponentsMainPhase() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyWhenAnotherPermanentWithTheSameNameAppearsBeforeResolution() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();
        harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Glorious Anthem")).hasSize(2)
                .noneMatch(p -> p.getCard().isToken());
        assertThat(yenna.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void copiesALegendaryEnchantmentWithoutApplyingTheLegendRule() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaGodOfTheHunt());

        activate(yenna, nylea);
        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Nylea, God of the Hunt")).hasSize(2)
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(yenna.isTapped()).isTrue();
    }

    @Test
    void copyingABestowedAuraCreatesACreatureWithoutUntappingOrScrying() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        prepareSorcerySpeedActivation();
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castWithAlternateCost(player1, 0, yenna.getId());
        harness.passBothPriorities();
        Permanent satyr = findPermanent(player1, "Boon Satyr");

        activate(yenna, satyr);

        assertThat(findPermanents(player1, "Boon Satyr")).filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(gqs.isCreature(gd, token)).isTrue();
                    assertThat(token.getAttachedTo()).isNull();
                });
        assertThat(yenna.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateWithoutPayingTwoMana() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent yenna = harness.addToBattlefieldAndReturn(player1, new YennaRedtoothRegent());
        yenna.setSummoningSick(true);
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        addManaForAbility();
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithASpellOnTheStack() {
        Permanent yenna = addCreatureReady(player1, new YennaRedtoothRegent());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        prepareSorcerySpeedActivation();
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(yenna), null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
    }

    private void activate(Permanent yenna, Permanent target) {
        addManaForAbility();
        prepareSorcerySpeedActivation();
        harness.activateAbility(player1, battlefieldIndex(yenna), null, target.getId());
        harness.passBothPriorities();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
