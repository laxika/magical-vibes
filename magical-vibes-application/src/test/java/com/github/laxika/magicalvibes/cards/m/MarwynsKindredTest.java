package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarwynsKindred.class, MarwynTheNurturer.class, LlanowarElves.class, Eviscerate.class})
class MarwynsKindredTest extends BaseCardTest {

    @Test
    void conjuresMarwynAndTheChosenNumberOfLlanowarElves() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Marwyn, the Nurturer", "Llanowar Elves", "Llanowar Elves");
        assertThat(battlefield).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void conjuredMarwynRetainsItsAbilities() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent marwyn = findPermanent(player1, "Marwyn, the Nurturer");
        marwyn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void conjuredElvesTriggerMarwynAndRetainTheirManaAbilities() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent marwyn = findPermanent(player1, "Marwyn, the Nurturer");
        assertThat(marwyn.getPlusOnePlusOneCounters()).isEqualTo(2);
        marwyn.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);

        findPermanents(player1, "Llanowar Elves").forEach(elf -> elf.setSummoningSick(false));
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void destroyedConjuredCreaturesRemainInTheGraveyard() {
        harness.setHand(player1, List.of(new MarwynsKindred()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent marwyn = findPermanent(player1, "Marwyn, the Nurturer");
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castSorcery(player1, 0, marwyn.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, elf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Marwyn, the Nurturer");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Marwyn, the Nurturer");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }
}
