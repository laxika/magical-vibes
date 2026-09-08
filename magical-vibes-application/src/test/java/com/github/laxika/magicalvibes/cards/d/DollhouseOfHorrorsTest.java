package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BondedConstruct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DollhouseOfHorrors.class, BondedConstruct.class, GrizzlyBears.class, LlanowarElves.class})
class DollhouseOfHorrorsTest extends BaseCardTest {

    @Test
    void createsAConstructArtifactTokenWithDynamicStatsAndHaste() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(token.getCard().getPower()).isZero();
        assertThat(token.getCard().getToughness()).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void countsOtherConstructsForTheTokenBonus() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.addToBattlefield(player1, new BondedConstruct());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void choosesTheCreatureWhenActivating() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        Card first = new GrizzlyBears();
        Card chosen = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, chosen));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardExileCostChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
