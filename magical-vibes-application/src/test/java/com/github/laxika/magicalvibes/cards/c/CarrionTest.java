package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carrion.class, CadaverousKnight.class, CharcoalDiamond.class})
class CarrionTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Carrion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Sacrificing a 2-power creature creates two 0/1 Insects")
    void sacrificeTwoPowerCreatureCreatesTwoInsects() {
        Permanent sacrifice = addCreatureReady(player1, new CadaverousKnight()); // 2/2
        prepare();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cadaverous Knight");
        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(insect.getCard().getColors()).containsExactly(CardColor.BLACK);
            assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(insect.getEffectivePower()).isZero();
            assertThat(insect.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Sacrificed creature's power counts +1/+1 counters")
    void sacrificedPowerIncludesCounters() {
        Permanent sacrifice = addCreatureReady(player1, new CadaverousKnight()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4); // 6/6
        prepare();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(6);
    }

    @Test
    @DisplayName("Must sacrifice a creature as an additional cost")
    void cannotCastWithoutSacrifice() {
        prepare();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CharcoalDiamond());
        prepare();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new CadaverousKnight());
        prepare();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
