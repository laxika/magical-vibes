package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObsidianObelisk.class, CoilingOracle.class})
class ObsidianObeliskTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new ObsidianObelisk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tappingAddsColorlessMana() {
        addReadyObelisk();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tappingAddsManaRestrictedToMulticoloredSpells() {
        addReadyObelisk();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCanCastMulticoloredSpell() {
        addReadyObelisk();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player1, List.of(testCreature("Multicolored Creature", CardColor.BLUE, CardColor.RED)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictedManaCannotCastMonocoloredSpell() {
        addReadyObelisk();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player1, List.of(testCreature("Monocolored Creature", CardColor.BLUE)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyObelisk() {
        harness.addToBattlefield(player1, new ObsidianObelisk());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachColorImmediately(ManaColor color) {
        addReadyObelisk();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyMana(color)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaPaysColoredCostOfRealMulticoloredSpell() {
        addReadyObelisk();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new CoilingOracle()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void restrictedManaCannotPayGenericCostOfColorlessSpell() {
        addReadyObelisk();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ObsidianObelisk()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getMulticoloredSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void tappedObeliskCannotActivateOtherManaAbility() {
        addReadyObelisk();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private static Card testCreature(String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{U}");
        card.setColors(List.of(colors));
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
