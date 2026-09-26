package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.m.MurmursFromBeyond;
import com.github.laxika.magicalvibes.cards.s.ScrollOfOrigins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YukiOnna.class, ScrollOfOrigins.class, GhostLitRedeemer.class,
        MurmursFromBeyond.class, ArabaMothrider.class})
class YukiOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying a target artifact")
    void entersByDestroyingTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrollOfOrigins());
        harness.setHand(player1, List.of(new YukiOnna()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Scroll of Origins");
        harness.assertOnBattlefield(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Yuki-Onna to its owner's hand")
    void spiritSpellReturnsYukiOnna() {
        addYukiOnna();
        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Yuki-Onna to its owner's hand")
    void arcaneSpellReturnsYukiOnna() {
        addYukiOnna();
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Yuki-Onna on the battlefield")
    void decliningCastTriggerLeavesYukiOnnaOnBattlefield() {
        addYukiOnna();
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Yuki-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addYukiOnna();
        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yuki-Onna");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ETB ability cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new YukiOnna()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enter without an artifact to target")
    void entersWithoutArtifactToTarget() {
        harness.castFromHand(player1, new YukiOnna(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yuki-Onna");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Yuki-Onna")
    void opponentSpiritSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new YukiOnna());

        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Yuki-Onna");
        assertThat(gd.stack).isEmpty();
    }

    private void addYukiOnna() {
        harness.addToBattlefield(player1, new YukiOnna());
    }
}
