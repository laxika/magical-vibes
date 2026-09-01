package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkeletalVampire.class)
class SkeletalVampireTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoFlyingBatTokens() {
        castSkeletalVampire();

        List<Permanent> bats = bats();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.getCard().getPower()).isEqualTo(1);
            assertThat(bat.getCard().getToughness()).isEqualTo(1);
            assertThat(bat.getCard().getSubtypes()).containsExactly(CardSubtype.BAT);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    void sacrificingABatCreatesTwoReplacementBatTokens() {
        castSkeletalVampire();
        Permanent bat = bats().getFirst();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bat.getId());
        harness.passBothPriorities();

        assertThat(bats()).hasSize(3);
    }

    @Test
    void sacrificingABatGivesSkeletalVampireARegenerationShield() {
        castSkeletalVampire();
        Permanent vampire = findPermanent(player1, "Skeletal Vampire");
        Permanent bat = bats().getFirst();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bat.getId());
        harness.passBothPriorities();

        assertThat(vampire.getRegenerationShield()).isEqualTo(1);
    }

    private void castSkeletalVampire() {
        harness.setHand(player1, List.of(new SkeletalVampire()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> bats() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BAT))
                .toList();
    }
}
