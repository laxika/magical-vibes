package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DirgurFocusmageBraingeyser.class, Braingeyser.class})
class DirgurFocusmageBraingeyserTest extends BaseCardTest {

    @Test
    void highManaValueInstantOrSorceryFromHandPreparesDirgur() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        Braingeyser braingeyser = new Braingeyser();
        harness.setHand(player1, List.of(braingeyser));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(dirgur.isPrepared()).isTrue();
        assertThat(dirgur.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(dirgur.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void lowerManaValueInstantOrSorceryDoesNotPrepareDirgur() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(dirgur.isPrepared()).isFalse();
        assertThat(dirgur.getPreparedSpellCardId()).isNull();
    }
}
