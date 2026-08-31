package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GaeasEmbrace;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({YennaRedtoothRegent.class, GloriousAnthem.class, GaeasEmbrace.class, GrizzlyBears.class})
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
        Permanent aura = new Permanent(new GaeasEmbrace());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        activate(yenna, aura);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(yenna.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
