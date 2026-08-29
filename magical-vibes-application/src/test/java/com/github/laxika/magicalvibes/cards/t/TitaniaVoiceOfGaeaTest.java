package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TitaniaVoiceOfGaeaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when a land card is put into its controller's graveyard")
    void gainsLifeWhenOwnLandIsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Melds with Argoth at upkeep and returns graveyard lands tapped")
    void meldsWithArgothAtUpkeep() {
        Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaVoiceOfGaea());
        harness.addToBattlefield(player1, new Forest());
        Permanent argoth = harness.addToBattlefieldAndReturn(player1, argoth());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gqs.isLand(gd, argoth)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .allMatch(card -> card.hasType(CardType.LAND));

        advanceToUpkeep(player1);
        assertThat(gd.stack).anyMatch(entry -> entry.getSourcePermanentId().equals(titania.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent melded = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof TitaniaGaeaIncarnate)
                .findFirst().orElseThrow();
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(4);
        assertThat(gqs.getEffectivePower(gd, melded)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, melded)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animates a target land with four +1/+1 counters permanently")
    void animatesTargetLand() {
        harness.addToBattlefield(player1, new TitaniaGaeaIncarnate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    private static Card argoth() {
        Card argoth = new Card();
        argoth.setName("Argoth, Sanctum of Nature");
        argoth.setType(CardType.LAND);
        argoth.setSubtypes(List.of(CardSubtype.FOREST));
        return argoth;
    }
}
