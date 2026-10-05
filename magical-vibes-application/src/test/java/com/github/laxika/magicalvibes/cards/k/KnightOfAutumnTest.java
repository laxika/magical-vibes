package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ExperimentalFrenzy;
import com.github.laxika.magicalvibes.cards.g.GatekeeperGargoyle;
import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.cards.l.LedevGuardian;
import com.github.laxika.magicalvibes.cards.s.SelesnyaLocket;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfAutumn.class, ExperimentalFrenzy.class, GatekeeperGargoyle.class, JusticeStrike.class,
        LedevGuardian.class, SelesnyaLocket.class})
class KnightOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts two +1/+1 counters on Knight of Autumn")
    void countersMode() {
        castKnight(0);
        resolveAllTriggers();

        Permanent knight = findPermanent(player1, "Knight of Autumn");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB mode destroys target artifact")
    void destroysArtifactMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GatekeeperGargoyle());

        castKnight(1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("ETB mode destroys target enchantment")
    void destroysEnchantmentMode() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ExperimentalFrenzy());

        castKnight(1, enchantment.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LedevGuardian());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SelesnyaLocket());
        castKnight();
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Ledev Guardian");
        harness.assertInGraveyard(player2, "Selesnya Locket");
    }

    @Test
    @DisplayName("ETB mode gains 4 life")
    void lifeMode() {
        harness.setLife(player1, 10);

        castKnight(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    private void castKnight(int mode) {
        castKnight(mode, null);
    }

    private void castKnight(int mode, java.util.UUID targetId) {
        castKnight();
        harness.handleListChoice(player1, switch (mode) {
            case 0 -> "Put two +1/+1 counters on Knight of Autumn";
            case 1 -> "Destroy target artifact or enchantment";
            case 2 -> "You gain 4 life";
            default -> throw new IllegalArgumentException("Unknown mode");
        });
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    private void castKnight() {
        harness.setHand(player1, List.of(new KnightOfAutumn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An entry without casting still offers the life mode")
    void lifeModeWhenNotCast() {
        harness.setLife(player1, 10);
        Permanent knight = harness.enterBattlefieldAndReturn(player1, new KnightOfAutumn());
        harness.handleListChoice(player1, "You gain 4 life");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters affect only the Knight that triggered the ability")
    void countersOnlyOnEnteringKnight() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new KnightOfAutumn());
        castKnight(0);
        resolveAllTriggers();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroy mode can target the controller's own artifact")
    void destroysOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SelesnyaLocket());
        castKnight(1, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Selesnya Locket");
        harness.assertOnBattlefield(player1, "Knight of Autumn");
    }

    @Test
    @DisplayName("Life mode resolves after Knight of Autumn dies in response")
    void lifeModeSurvivesSourceRemoval() {
        harness.setLife(player1, 10);
        castKnight(2);
        Permanent knight = findPermanent(player1, "Knight of Autumn");
        harness.setHand(player2, List.of(new JusticeStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, knight.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Knight of Autumn");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroy mode does not switch modes when its target leaves")
    void destroyModeDoesNotSwitchWhenTargetLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ExperimentalFrenzy());
        castKnight(1, enchantment.getId());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Experimental Frenzy");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "Knight of Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
