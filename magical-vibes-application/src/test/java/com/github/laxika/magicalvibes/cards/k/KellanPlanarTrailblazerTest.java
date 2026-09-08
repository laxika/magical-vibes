package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KellanPlanarTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("The Scout ability becomes a Detective without changing base stats")
    void scoutAbilityBecomesDetective() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kellan.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.DETECTIVE)
                .doesNotContain(CardSubtype.SCOUT);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Detective ability becomes a 3/2 Rogue with double strike")
    void detectiveAbilityBecomesRogueWithDoubleStrike() {
        Permanent kellan = addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kellan.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.FAERIE, CardSubtype.ROGUE)
                .doesNotContain(CardSubtype.SCOUT, CardSubtype.DETECTIVE);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kellan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The Detective combat-damage ability exiles the top card with play permission")
    void combatDamageExilesTopCardToPlay() {
        addCreatureReady(player1, new KellanPlanarTrailblazer());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }
}
