package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flusterstorm.class, GrizzlyBears.class, MightOfOaks.class, CounselOfTheSoratami.class})
class FlusterstormTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant when its controller cannot pay {1}")
    void countersInstantWhenControllerCannotPay() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        resolveFlusterstorm(false);

        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("The targeted instant resolves when its controller pays {1}")
    void instantResolvesWhenControllerPays() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        resolveFlusterstorm(true);

        assertThat(bear.getEffectivePower()).isEqualTo(9);
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Flusterstorm")
    void stormCopiesForEachPriorSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    @DisplayName("Counters a target instant or sorcery spell")
    void countersTargetInstantOrSorcerySpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        resolveFlusterstorm(false);

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Flusterstorm");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Flusterstorm")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Flusterstorm()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).filteredOn(StackEntry::isCopy).hasSize(3);
    }

    private void resolveFlusterstorm(boolean pay) {
        for (int i = 0; i < 12 && (!gd.stack.isEmpty()
                || gd.interaction.activeInteraction() != null); i++) {
            PendingInteraction.MayAbilityChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
            if (choice != null) {
                harness.handleMayAbilityChosen(choice.playerId().equals(player1.getId())
                        ? player1 : player2, pay && choice.playerId().equals(player1.getId()));
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
