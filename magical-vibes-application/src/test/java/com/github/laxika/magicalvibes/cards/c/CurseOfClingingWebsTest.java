package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CurseOfClingingWebs.class, GrizzlyBears.class, Shock.class})
class CurseOfClingingWebsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nontoken creature enchanted player controls and creates a Spider")
    void exilesDyingCreatureAndCreatesSpider() {
        placeCurseOnPlayer2();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithShock(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getCard().getId()));
        Permanent spider = findPermanent(player1, "Spider");
        assertThat(spider.getEffectivePower()).isEqualTo(1);
        assertThat(spider.getEffectiveToughness()).isEqualTo(2);
        assertThat(spider.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
        assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
    }

    @Test
    @DisplayName("Does not trigger for a creature the Curse controller controls")
    void doesNotTriggerForCurseControllersCreature() {
        placeCurseOnPlayer2();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithShock(bears);

        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(bears.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
        assertThat(findPermanents(player1, "Spider")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a token creature enchanted player controls")
    void doesNotTriggerForTokenCreature() {
        placeCurseOnPlayer2();
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        destroyWithShock(token);

        assertThat(findPermanents(player1, "Spider")).isEmpty();
    }

    private void placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        curse.setAttachedTo(player2.getId());
    }

    private void destroyWithShock(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
