package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulSeparatorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the creature card and creates a 1/1 flying Spirit copy plus a stat-matched black Zombie")
    void createsSpiritCopyAndZombieToken() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(bears.getId()));

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Grizzly Bears".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);

        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Zombie".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Activating sacrifices Soul Separator as a cost")
    void activationSacrificesSource() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card separatorCard = separator.getCard();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        harness.activateAbilityWithGraveyardTargets(player1, idx, 0, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(separatorCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(separatorCard.getId()));
    }

    @Test
    @DisplayName("Rejects a noncreature card in the graveyard as a target")
    void rejectsNonCreatureTarget() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card land = new Plains();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, idx, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent separator = harness.addToBattlefieldAndReturn(player1, new SoulSeparator());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(separator);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, idx, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
