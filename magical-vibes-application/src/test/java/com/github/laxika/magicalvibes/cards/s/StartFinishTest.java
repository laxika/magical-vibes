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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StartFinish.class, GrizzlyBears.class, Plains.class, RagingGoblin.class})
class StartFinishTest extends BaseCardTest {

    @Test
    @DisplayName("Start creates two 1/1 white Warrior tokens with vigilance")
    void startCreatesWarriorTokensWithVigilance() {
        harness.setHand(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

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

    @Test
    @DisplayName("Finish can target and sacrifice the same creature, then is exiled without resolving")
    void finishCanSacrificeItsOwnTarget() {
        harness.setHand(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        List<Permanent> warriors = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(warriors).hasSize(2);
        Permanent sacrificedTarget = warriors.getFirst();
        Permanent survivor = warriors.getLast();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithSacrifice(player1, 0, sacrificedTarget.getId(), sacrificedTarget.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).containsExactly(survivor.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).containsExactly(survivor.getId());
        harness.assertNotInGraveyard(player1, "Start");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Start"));
    }

    @Test
    @DisplayName("Finish can destroy another creature its caster controls and pays the sacrifice before resolution")
    void finishCanDestroyAnotherControlledCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithSacrifice(player1, 0, target.getId(), fodder.getId());

        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Start"));
    }

    @Test
    @DisplayName("Finish cannot sacrifice an opponent's creature")
    void finishCannotSacrificeOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StartFinish()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(ownCreature.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Start");
        assertThat(gd.stack).isEmpty();
    }
}
