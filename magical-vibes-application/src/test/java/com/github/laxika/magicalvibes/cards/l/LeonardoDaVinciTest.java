package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeonardoDaVinci.class, DarksteelRelic.class, GrizzlyBears.class})
class LeonardoDaVinciTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability sets your Thopters to your hand size")
    void firstAbilitySetsThopterPowerAndToughnessToHandSize() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoDaVinci());
        Permanent thopter = addThopter();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarksteelRelic(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(leonardo),
                0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.setHand(player1, List.of(new DarksteelRelic(), new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second ability exiles a discarded artifact and creates its Thopter copy")
    void secondAbilityExilesArtifactAndCreatesThopterCopy() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoDaVinci());
        DarksteelRelic relic = new DarksteelRelic();
        harness.setHand(player1, List.of(new GrizzlyBears(), relic));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(leonardo),
                1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(relic);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(relic);

        Permanent token = findPermanent(player1, "Darksteel Relic");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The second ability does not copy a discarded nonartifact")
    void secondAbilityDoesNotCopyNonartifact() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoDaVinci());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new DarksteelRelic(), bears));
        harness.setLibrary(player1, List.of(new DarksteelRelic()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(leonardo),
                1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().isToken() && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    private Permanent addThopter() {
        Card card = new Card();
        card.setName("Thopter");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.THOPTER));
        card.setToken(true);
        card.setKeywords(Set.of(Keyword.FLYING));
        return addCreatureReady(player1, card);
    }
}
