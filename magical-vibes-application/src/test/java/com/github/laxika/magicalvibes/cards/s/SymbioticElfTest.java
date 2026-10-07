package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioticElf.class, SolarBlast.class})
class SymbioticElfTest extends BaseCardTest {

    @Test
    void createsTwoGreenInsectsWhenItDies() {
        harness.addToBattlefield(player1, new SymbioticElf());

        harness.setHand(player2, List.of(new SolarBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Symbiotic Elf"));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Insect"))
                .toList();
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().getPower()).isEqualTo(1);
            assertThat(insect.getCard().getToughness()).isEqualTo(1);
            assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(insect.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        });
    }

    @Test
    void enteringTheBattlefieldDoesNotCreateInsects() {
        harness.enterBattlefieldAndReturn(player1, new SymbioticElf());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Symbiotic Elf");
    }

    @Test
    void createsInsectsForTheOpponentWhenTheirElfDies() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new SymbioticElf());
        elf.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Symbiotic Elf");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().getName()).isEqualTo("Insect");
            assertThat(insect.isTapped()).isFalse();
        });
    }

    @Test
    void eachElfCreatesTwoInsectsWhenTheyDieTogether() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SymbioticElf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SymbioticElf());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().getName()).isEqualTo("Insect");
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
