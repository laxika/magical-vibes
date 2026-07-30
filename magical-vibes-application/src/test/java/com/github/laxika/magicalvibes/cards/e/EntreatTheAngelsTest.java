package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntreatTheAngelsTest extends BaseCardTest {

    private List<Permanent> angels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Angel"))
                .toList();
    }

    @Test
    @DisplayName("Cast for {X}{X}{W}{W}{W} with X=2 creates two 4/4 flying Angels")
    void hardCastCreatesXAngels() {
        harness.setHand(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(angels()).hasSize(2);
        assertThat(angels()).allSatisfy(angel -> {
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        // X is paid twice: 2*2 generic + {W}{W}{W}.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cast with X=0 creates no tokens")
    void hardCastWithZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(angels()).isEmpty();
    }

    @Test
    @DisplayName("Miracle cast for {X}{W}{W} announces X and creates that many Angels")
    void miracleCastAnnouncesX() {
        harness.setLibrary(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);

        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities(); // resolve Entreat the Angels

        assertThat(angels()).hasSize(3);
        // {3}{W}{W} paid out of 2 white + 3 colorless.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Miracle cast with X=0 creates no tokens and spends only the coloured mana")
    void miracleCastWithZero() {
        harness.setLibrary(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(angels()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
