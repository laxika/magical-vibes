package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoingCavern.class, ElvishElegy.class})
class EchoingCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Echoing Cavern enters stores that type")
    void choosesCreatureTypeOnEntry() {
        harness.setHand(player1, List.of(new EchoingCavern()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Echoing Cavern").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The land produces colorless mana")
    void producesColorlessMana() {
        Permanent cavern = addCavern(CardSubtype.BEAR);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cavern.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability produces mana restricted to the chosen creature type")
    void producesChosenTypeMana() {
        addCavern(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.MERFOLK), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The restricted mana can cast a noncreature spell of the chosen type")
    void restrictedManaCastsNoncreatureChosenTypeSpell() {
        addCavern(CardSubtype.ELF);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new ElvishElegy()));

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Elvish Elegy"));
    }

    @Test
    @DisplayName("Exhaust seeks a card of the chosen creature type")
    void exhaustSeeksChosenTypeCard() {
        Permanent cavern = addCavern(CardSubtype.ELF);
        ElvishElegy elegy = new ElvishElegy();
        harness.setLibrary(player1, List.of(elegy));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(cavern.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elegy);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        Permanent cavern = addCavern(CardSubtype.BEAR);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        cavern.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addCavern(CardSubtype chosenSubtype) {
        Permanent cavern = new Permanent(new EchoingCavern());
        cavern.setChosenSubtype(chosenSubtype);
        cavern.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cavern);
        return cavern;
    }
}
