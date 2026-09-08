package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VenserCorpsePuppetTest extends BaseCardTest {

    private static final String CREATE_SENTINEL =
            "If you don't control a creature named The Hollow Sentinel, create The Hollow Sentinel";
    private static final String GRANT_KEYWORDS =
            "Target artifact creature you control gains flying and lifelink until end of turn";

    @Test
    @DisplayName("Proliferating can create The Hollow Sentinel when none is controlled")
    void createsHollowSentinelWhenNoneIsControlled() {
        harness.addToBattlefield(player1, new VenserCorpsePuppet());

        proliferate();
        harness.handleListChoice(player1, CREATE_SENTINEL);

        Permanent sentinel = findPermanent(player1, "The Hollow Sentinel");
        assertThat(sentinel.getEffectivePower()).isEqualTo(3);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);
        assertThat(sentinel.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(sentinel.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Proliferating can grant flying and lifelink to a targeted artifact creature")
    void grantsFlyingAndLifelinkToTargetArtifactCreature() {
        harness.addToBattlefield(player1, new VenserCorpsePuppet());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        proliferate();
        harness.handleListChoice(player1, GRANT_KEYWORDS);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(thopter.getId());

        harness.handlePermanentChosen(player1, thopter.getId());

        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The token mode does not create a second Sentinel when one is controlled")
    void doesNotCreateSecondSentinel() {
        harness.addToBattlefield(player1, new VenserCorpsePuppet());

        proliferate();
        harness.handleListChoice(player1, CREATE_SENTINEL);

        proliferate();
        harness.handleListChoice(player1, CREATE_SENTINEL);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("The Hollow Sentinel")))
                .hasSize(1);
    }

    private void proliferate() {
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
