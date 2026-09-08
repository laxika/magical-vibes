package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingbladeDisciple.class, LightningBolt.class})
class WingbladeDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Flurry creates one white Bird token with flying on the second spell only")
    void flurryCreatesBirdOnSecondSpellOnly() {
        addCreatureReady(player1, new WingbladeDisciple());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bird")).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getCard().getPower()).isEqualTo(1);
        assertThat(bird.getCard().getToughness()).isEqualTo(1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(bird.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(bird.getCard().isToken()).isTrue();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }
}
