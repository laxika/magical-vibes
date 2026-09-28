package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MultiversalRecruitment.class, GrizzlyBears.class})
class MultiversalRecruitmentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a creature you control")
    void createsTokenCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFromHand(target.getId());

        Permanent token = token();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes legendary from the token copy")
    void removesLegendaryFromTokenCopy() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent target = harness.addToBattlefieldAndReturn(player1, legendaryBears);
        castFromHand(target.getId());

        assertThat(token().getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultiversalRecruitment()));
        addManaForHandCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback creates the token and exiles the spell")
    void flashbackCreatesTokenAndExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MultiversalRecruitment()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(token()).isNotNull();
        harness.assertNotInGraveyard(player1, "Multiversal Recruitment");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Multiversal Recruitment"));
    }

    private void castFromHand(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MultiversalRecruitment()));
        addManaForHandCast();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addManaForHandCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
