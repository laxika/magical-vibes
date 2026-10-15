package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowPuppeteers.class, GrizzlyBears.class})
class ShadowPuppeteersTest extends BaseCardTest {

    @Test
    void entersWithTwoFlyingFaerieRogues() {
        harness.enterBattlefieldAndReturn(player1, new ShadowPuppeteers());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.FAERIE, CardSubtype.ROGUE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void flyingAttackerMayBecomeRedDragon() {
        Permanent shadow = harness.enterBattlefieldAndReturn(player1, new ShadowPuppeteers());
        shadow.setSummoningSick(false);
        harness.passBothPriorities();
        Permanent attacker = findPermanent(player1, "Faerie Rogue");
        attacker.setSummoningSick(false);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, attacker)).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(gqs.hasEffectiveSubtype(gd, attacker, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }
}
