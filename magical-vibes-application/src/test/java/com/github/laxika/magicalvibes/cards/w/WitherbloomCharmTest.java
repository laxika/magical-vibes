package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitherbloomCharmTest extends BaseCardTest {

    private void addBG() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    

    @Nested
    @DisplayName("Mode 0: You may sacrifice a permanent, if you do draw two")
    class SacrificeDrawMode {

        @Test
        @DisplayName("Sacrificing a permanent draws two cards")
        void sacrificeDrawsTwo() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new WitherbloomCharm()));
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            addBG();

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Declining sacrifices nothing and draws nothing")
        void declineDrawsNothing() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new WitherbloomCharm()));
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
            addBG();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mode 1: You gain 5 life")
    class GainLifeMode {

        @Test
        @DisplayName("Controller gains 5 life")
        void gainsFiveLife() {
            harness.setHand(player1, List.of(new WitherbloomCharm()));
            addBG();
            int before = gd.playerLifeTotals.get(player1.getId());

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(before + 5);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target nonland permanent with mana value 2 or less")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a nonland permanent with mana value 2 or less")
        void destroysLowManaValuePermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new WitherbloomCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target a permanent with mana value greater than 2")
        void cannotTargetHighManaValue() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new WitherbloomCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Enormous Baloth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
