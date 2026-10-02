package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LothlRienLookout.class, GrizzlyBears.class})
class LothlRienLookoutTest extends BaseCardTest {

    @Test
    void attacksAndTriggersScryOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new LothlRienLookout());
        lookout.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lookout)));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);
    }
}
