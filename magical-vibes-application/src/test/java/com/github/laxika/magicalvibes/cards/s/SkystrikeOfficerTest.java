package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkystrikeOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 colorless Soldier artifact creature token")
    void attackingCreatesSoldierToken() {
        addCreatureReady(player1, new SkystrikeOfficer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Tapping three Soldiers draws a card")
    void tappingThreeSoldiersDrawsCard() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .filter(Permanent::isTapped)
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability cannot be activated without three untapped Soldiers")
    void requiresThreeUntappedSoldiers() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new YotianSoldier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
