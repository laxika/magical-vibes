package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KorozdaGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{B}{G}: target creature gets +1/+1 and gains intimidate until end of turn")
    void pumpsAndGrantsIntimidate() {
        addCreatureReady(player1, new KorozdaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Pump and intimidate wear off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KorozdaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a 3/3 creates three 1/1 green Saprolings")
    void sacrificeCreatesTokensEqualToToughness() {
        addCreatureReady(player1, new KorozdaGuildmage());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Saproling"))
                .hasSize(3)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
                    assertThat(p.getCard().isToken()).isTrue();
                });
    }

    @Test
    @DisplayName("Can sacrifice Korozda Guildmage itself for two Saprolings")
    void canSacrificeSelf() {
        Permanent guildmage = addCreatureReady(player1, new KorozdaGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Korozda Guildmage");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Saproling"))
                .hasSize(2);
        assertThat(guildmage.getId()).isNotIn(
                gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("Cannot sacrifice a token creature to the Saproling ability")
    void cannotSacrificeToken() {
        addCreatureReady(player1, new KorozdaGuildmage());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Only the guildmage itself is a legal nontoken sacrifice; auto-pays.
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Korozda Guildmage");
        harness.assertOnBattlefield(player1, "Saproling Token");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Saproling".equals(p.getCard().getName()))
                .hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate Saproling ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new KorozdaGuildmage());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
