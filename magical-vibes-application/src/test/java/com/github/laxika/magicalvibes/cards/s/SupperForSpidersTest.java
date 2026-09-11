package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupperForSpiders.class, GrizzlyBears.class, LlanowarElves.class, LightningBolt.class})
class SupperForSpidersTest extends BaseCardTest {

    @Test
    void returnsEligibleOpponentCreaturesAsFoodArtifacts() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SupperForSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(returned.getCard().getAdditionalTypes()).isEmpty();
        assertThat(returned.getCard().getSubtypes()).containsExactly(CardSubtype.FOOD);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void retainsOriginalAbilitiesAndCanBeSacrificedForFood() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SupperForSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        returned.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanents(player1, "Llanowar Elves")).isEmpty();
        assertThat(findPermanents(player2, "Llanowar Elves")).isEmpty();
    }
}
