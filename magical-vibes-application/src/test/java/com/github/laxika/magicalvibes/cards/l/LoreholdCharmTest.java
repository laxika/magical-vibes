package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoreholdCharmTest extends BaseCardTest {

    private void addRW() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Has a ChooseOneEffect with three options")
    void hasCorrectEffects() {
        LoreholdCharm card = new LoreholdCharm();

        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.options()).hasSize(3);
        assertThat(effect.options().get(0).effect()).isInstanceOf(EachOpponentSacrificesPermanentsEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        assertThat(effect.options().get(2).effects().get(0)).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(effect.options().get(2).effects().get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    @Nested
    @DisplayName("Mode 0: Each opponent sacrifices a nontoken artifact")
    class SacrificeArtifactMode {

        @Test
        @DisplayName("Opponent sacrifices their only artifact")
        void opponentSacrificesArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Reanimate an artifact or creature card with mana value 2 or less")
    class ReanimateMode {

        @Test
        @DisplayName("Returns a mana value 2 creature card from graveyard to battlefield")
        void reanimatesLowManaValueCreature() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target a card with mana value greater than 2")
        void cannotTargetHighManaValue() {
            Card baloth = new EnormousBaloth();
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, new ArrayList<>(List.of(baloth, bears)));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, baloth.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control get +1/+1 and gain trample")
    class AnthemMode {

        @Test
        @DisplayName("Boosts your creatures and grants trample")
        void boostsAndGrantsTrample() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Does not boost opponent creatures")
        void doesNotBoostOpponentCreatures() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        }
    }
}
