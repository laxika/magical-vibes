package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({IcingdeathFrostTyrant.class, GrizzlyBears.class, DoomBlade.class})
class IcingdeathFrostTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Icingdeath dies, it creates a legendary white Equipment token")
    void deathCreatesFrostTongue() {
        harness.addToBattlefield(player1, new IcingdeathFrostTyrant());

        killWithDoomBlade(player2, player1, "Icingdeath, Frost Tyrant");
        harness.passBothPriorities();

        Permanent token = frostTongue();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.EQUIPMENT);
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Frost Tongue equips for {2} and gives the equipped creature +2/+0")
    void equipsAndBoostsCreature() {
        Permanent token = createFrostTongue();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, tokenIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(token.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Frost Tongue taps a target creature defending player controls when equipped creature attacks")
    void attackTriggerTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = createFrostTongue();
        token.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    private Permanent createFrostTongue() {
        harness.addToBattlefield(player1, new IcingdeathFrostTyrant());
        killWithDoomBlade(player2, player1, "Icingdeath, Frost Tyrant");
        harness.passBothPriorities();
        return frostTongue();
    }

    private Permanent frostTongue() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Icingdeath, Frost Tongue"))
                .findFirst()
                .orElseThrow();
    }

    private void killWithDoomBlade(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, harness.getPermanentId(targetController, targetName));
        harness.passBothPriorities();
    }
}
