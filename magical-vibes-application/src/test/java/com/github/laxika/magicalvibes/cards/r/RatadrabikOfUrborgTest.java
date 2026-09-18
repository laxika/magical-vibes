package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatadrabikOfUrborg.class, CaptainSisay.class, GluttonousZombie.class,
        GrizzlyBears.class, Shock.class})
class RatadrabikOfUrborgTest extends BaseCardTest {

    @Test
    void givesOtherZombiesYouControlVigilance() {
        Permanent ratadrabik = harness.addToBattlefieldAndReturn(player1, new RatadrabikOfUrborg());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GluttonousZombie());
        Permanent nonZombie = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingZombie = harness.addToBattlefieldAndReturn(player2, new GluttonousZombie());

        assertThat(gqs.hasKeyword(gd, ratadrabik, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonZombie, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingZombie, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void createsNonlegendaryBlackZombieCopyOfLegendaryCreature() {
        harness.addToBattlefield(player1, new RatadrabikOfUrborg());
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());

        killWithShock(legendaryCreature.getId());
        resolveAllTriggers();

        Permanent copy = findPermanents(player1, "Captain Sisay").getFirst();
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(copy.getCard().getColors()).contains(CardColor.BLACK);
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForNonlegendaryCreature() {
        harness.addToBattlefield(player1, new RatadrabikOfUrborg());
        Permanent nonlegendaryCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(nonlegendaryCreature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void usesLastKnownInformationForLegendaryToken() {
        harness.addToBattlefield(player1, new RatadrabikOfUrborg());
        Card legendaryTokenCard = new CaptainSisay();
        legendaryTokenCard.setToken(true);
        Permanent legendaryToken = harness.addToBattlefieldAndReturn(player1, legendaryTokenCard);

        killWithShock(legendaryToken.getId());
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Captain Sisay");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().isToken()).isTrue();
        assertThat(copies.getFirst().getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    private void killWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }
}
