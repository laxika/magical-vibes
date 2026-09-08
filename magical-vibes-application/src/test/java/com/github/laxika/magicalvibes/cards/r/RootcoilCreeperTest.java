package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.r.RollingTemblor;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootcoilCreeper.class, GrizzlyBears.class, SqueeTheImmortal.class, RollingTemblor.class, HolyDay.class})
class RootcoilCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void addsAnyColorMana() {
        addCreatureReady(player1, new RootcoilCreeper());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two mana of one color restricted to graveyard spells")
    void addsGraveyardOnlyMana() {
        addCreatureReady(player1, new RootcoilCreeper());
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SqueeTheImmortal);
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Graveyard-only mana cannot pay for a spell cast from hand")
    void graveyardOnlyManaCannotPayForSpellFromHand() {
        addCreatureReady(player1, new RootcoilCreeper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability returns a flashback card from exile and exiles Rootcoil Creeper")
    void returnsFlashbackCardFromExile() {
        Permanent rootcoil = addCreatureReady(player1, new RootcoilCreeper());
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player1, List.of(flashbackCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, flashbackCard.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rootcoil);
        assertThat(gd.playerHands.get(player1.getId())).contains(flashbackCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(flashbackCard);
    }

    @Test
    @DisplayName("The third ability rejects an exiled card without flashback")
    void rejectsExiledCardWithoutFlashback() {
        addCreatureReady(player1, new RootcoilCreeper());
        Card cardWithoutFlashback = new HolyDay();
        harness.setExile(player1, List.of(cardWithoutFlashback));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, cardWithoutFlashback.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
