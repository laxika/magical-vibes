package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AggressiveUrge;
import com.github.laxika.magicalvibes.cards.a.AgonizingDemise;
import com.github.laxika.magicalvibes.cards.b.BogInitiate;
import com.github.laxika.magicalvibes.cards.c.CursedFlesh;
import com.github.laxika.magicalvibes.cards.l.LlanowarCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarKnight.class, LlanowarCavalry.class, AgonizingDemise.class,
        AggressiveUrge.class, BogInitiate.class, CursedFlesh.class})
class LlanowarKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells cannot target Llanowar Knight")
    void blackSpellsCannotTargetLlanowarKnight() {
        Permanent knight = addCreatureReady(player1, new LlanowarKnight());
        addCreatureReady(player1, new LlanowarCavalry());

        harness.setHand(player2, List.of(new AgonizingDemise()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Black creatures cannot block Llanowar Knight")
    void blackCreaturesCannotBlockLlanowarKnight() {
        addCreatureReady(player1, new LlanowarKnight());
        addCreatureReady(player2, new BogInitiate());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Llanowar Knight takes no combat damage from black creatures")
    void takesNoCombatDamageFromBlackCreatures() {
        addCreatureReady(player1, new BogInitiate());
        addCreatureReady(player2, new LlanowarKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Llanowar Knight");
        harness.assertInGraveyard(player1, "Bog Initiate");
    }

    @Test
    @DisplayName("Black Auras cannot enchant Llanowar Knight")
    void blackAurasCannotEnchantLlanowarKnight() {
        Permanent knight = addCreatureReady(player2, new LlanowarKnight());
        addCreatureReady(player2, new LlanowarCavalry());

        harness.setHand(player1, List.of(new CursedFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Nonblack spells can target Llanowar Knight")
    void nonblackSpellsCanTargetLlanowarKnight() {
        Permanent knight = addCreatureReady(player1, new LlanowarKnight());
        harness.setLibrary(player1, List.of(new LlanowarCavalry()));
        harness.setHand(player1, List.of(new AggressiveUrge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, knight.getId());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        harness.assertInHand(player1, "Llanowar Cavalry");
    }
}
