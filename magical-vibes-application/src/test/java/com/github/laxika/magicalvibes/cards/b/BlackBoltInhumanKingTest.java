package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackBoltInhumanKing.class, GrizzlyBears.class, LightningBolt.class, ZuranSpellcaster.class})
class BlackBoltInhumanKingTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 when its controller casts a noncreature spell")
    void boostsForNoncreatureSpell() {
        Permanent blackBolt = addCreatureReady(player1, new BlackBoltInhumanKing());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(blackBolt.getEffectivePower()).isEqualTo(5);
        assertThat(blackBolt.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blackBolt = addCreatureReady(player1, new BlackBoltInhumanKing());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(blackBolt.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackBolt.getEffectivePower()).isEqualTo(3);
        assertThat(blackBolt.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal Voice destroys a nonland permanent controlled by the opposing spell caster")
    void destroysPermanentWhenTargetedByOpponentSpell() {
        Permanent blackBolt = addCreatureReady(player1, new BlackBoltInhumanKing());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        forceOpponentMainPhase();

        harness.castInstant(player2, 0, blackBolt.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Black Bolt, Inhuman King");
    }

    @Test
    @DisplayName("Lethal Voice also triggers when an opponent ability targets it")
    void destroysPermanentWhenTargetedByOpponentAbility() {
        Permanent blackBolt = addCreatureReady(player1, new BlackBoltInhumanKing());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new ZuranSpellcaster());
        forceOpponentMainPhase();

        harness.activateAbility(player2, 1, null, blackBolt.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Black Bolt, Inhuman King");
    }

    private void forceOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
