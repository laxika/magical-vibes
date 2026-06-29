package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledLandSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HowlOfTheNightPackTest extends BaseCardTest {

    @Test
    @DisplayName("Has CreateTokensPerControlledLandSubtypeEffect on resolution")
    void hasCorrectEffect() {
        HowlOfTheNightPack card = new HowlOfTheNightPack();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CreateTokensPerControlledLandSubtypeEffect.class);

        CreateTokensPerControlledLandSubtypeEffect effect =
                (CreateTokensPerControlledLandSubtypeEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.landSubtype()).isEqualTo(CardSubtype.FOREST);
        assertThat(effect.tokenName()).isEqualTo("Wolf");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates one 2/2 Wolf token per Forest controlled")
    void createsTokensPerForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(3);
        for (Permanent wolf : wolves) {
            assertThat(wolf.getCard().getPower()).isEqualTo(2);
            assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Creates no tokens when controller has no Forests")
    void noTokensWhenNoForests() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).isEmpty();
    }

    @Test
    @DisplayName("Does not count opponent's Forests")
    void doesNotCountOpponentForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(1);
    }
}
