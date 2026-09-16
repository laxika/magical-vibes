package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.c.CephalidRetainer;
import com.github.laxika.magicalvibes.cards.e.EngulfingFlames;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellbaneCentaur.class, AetherBurst.class, CephalidRetainer.class,
        EngulfingFlames.class, WildMongrel.class})
class SpellbaneCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Blue spells cannot target Spellbane Centaur")
    void blueSpellsCannotTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());

        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Spellbane Centaur")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue");
    }

    @Test
    @DisplayName("Blue spells cannot target another creature you control")
    void blueSpellsCannotTargetAnotherCreatureYouControl() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());
        harness.addToBattlefield(player2, new WildMongrel());

        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Wild Mongrel")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of blue");
    }

    @Test
    @DisplayName("Abilities from blue sources cannot target Spellbane Centaur")
    void blueSourceAbilitiesCannotTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());

        addCreatureReady(player1, new CephalidRetainer());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Spellbane Centaur")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue");
    }

    @Test
    @DisplayName("Abilities from blue sources cannot target another creature you control")
    void blueSourceAbilitiesCannotTargetAnotherCreatureYouControl() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());
        harness.addToBattlefield(player2, new WildMongrel());

        addCreatureReady(player1, new CephalidRetainer());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Wild Mongrel")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue");
    }

    @Test
    @DisplayName("Blue spells can target creatures an opponent controls")
    void blueSpellsCanTargetOpponentsCreatures() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());
        harness.addToBattlefield(player1, new WildMongrel());

        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wild Mongrel"));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Aether Burst"));
    }

    @Test
    @DisplayName("Non-blue spells can target Spellbane Centaur")
    void nonBlueSpellsCanTarget() {
        harness.addToBattlefield(player2, new SpellbaneCentaur());

        harness.setHand(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Spellbane Centaur"));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Engulfing Flames"));
    }
}
