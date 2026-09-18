package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatonhnhakTon.class, DarksteelAxe.class})
class RatonhnhakTonTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof and can't be blocked before dealing damage")
    void hasInitialProtectionAndEvasion() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());

        assertThat(gqs.hasKeyword(gd, ratonhnhak, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, ratonhnhak)).isTrue();
    }

    @Test
    @DisplayName("Loses hexproof and can't-be-blocked after dealing damage")
    void losesInitialAbilitiesAfterDealingDamage() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());
        ratonhnhak.setAttacking(true);

        resolveCombat();

        assertThat(gqs.hasKeyword(gd, ratonhnhak, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, ratonhnhak)).isFalse();
    }

    @Test
    @DisplayName("Creates a menacing Assassin and returns an Equipment attached to it")
    void createsAssassinAndReturnsEquipmentAttachedToIt() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());
        ratonhnhak.setAttacking(true);
        DarksteelAxe axe = new DarksteelAxe();
        harness.setGraveyard(player1, List.of(axe));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        resolveAllTriggers();

        Permanent assassin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ASSASSIN))
                .findFirst().orElseThrow();
        Permanent returnedAxe = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(axe.getId()))
                .findFirst().orElseThrow();

        assertThat(assassin.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(returnedAxe.getAttachedTo()).isEqualTo(assassin.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
