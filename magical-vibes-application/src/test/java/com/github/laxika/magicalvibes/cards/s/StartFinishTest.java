package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartFinishTest extends BaseCardTest {

    @Test
    @DisplayName("Start creates two 1/1 white Warrior tokens with vigilance")
    void startCreatesWarriorTokensWithVigilance() {
        harness.setHand(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> warriors = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Warrior"))
                .toList();
        assertThat(warriors).hasSize(2);
        for (Permanent warrior : warriors) {
            assertThat(warrior.getEffectivePower()).isEqualTo(1);
            assertThat(warrior.getEffectiveToughness()).isEqualTo(1);
            assertThat(warrior.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(warrior.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isTrue();
        }
        harness.assertInGraveyard(player1, "Start");
    }

    @Test
    @DisplayName("Finish sacrifices a creature, destroys target, then exiles")
    void finishSacrificesDestroysAndExiles() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithSacrifice(player1, 0, target.getId(), fodder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Start") || c.getName().equals("Finish"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Start"));
    }

    @Test
    @DisplayName("Finish cannot cast without a creature to sacrifice")
    void finishRequiresSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Finish cannot target a noncreature")
    void finishCannotTargetNoncreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, plains.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Finish requires sorcery timing")
    void finishRequiresSorceryTiming() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, target.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }
}
