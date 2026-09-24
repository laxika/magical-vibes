package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OthelmSigardianOutcast.class, GrizzlyBears.class, DoomBlade.class, Forest.class})
class OthelmSigardianOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature that died this turn to the battlefield tapped")
    void returnsCreatureThatDiedThisTurnTapped() {
        Permanent othelm = addCreatureReady(player1, new OthelmSigardianOutcast());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int othelmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(othelm);
        harness.activateAbility(player1, othelmIndex, 0, null, bears.getCard().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(othelm.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature that was not put into the graveyard from the battlefield this turn")
    void rejectsCreatureNotPutIntoGraveyardFromBattlefieldThisTurn() {
        Permanent othelm = addCreatureReady(player1, new OthelmSigardianOutcast());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int othelmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(othelm);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, othelmIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("from the battlefield this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void rejectsNonCreatureCard() {
        Permanent othelm = addCreatureReady(player1, new OthelmSigardianOutcast());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int othelmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(othelm);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, othelmIndex, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }
}
