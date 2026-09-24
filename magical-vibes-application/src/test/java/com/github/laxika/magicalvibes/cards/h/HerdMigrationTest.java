package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerdMigration.class, Forest.class, Plains.class, Swamp.class, GrizzlyBears.class})
class HerdMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 green Beast for each distinct basic land type you control")
    void createsBeastPerBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        castHerdMigration();

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(3);
        Permanent beast = beasts.getFirst();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("The hand ability searches for a basic land, gains 3 life, and discards Herd Migration")
    void handAbilitySearchesAndGainsLife() {
        HerdMigration migration = new HerdMigration();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(migration));
        harness.setLibrary(player1, List.of(forest, plains, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(migration);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, plains);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Creates no tokens when you control no basic land types")
    void createsNoTokensWithoutBasicLands() {
        castHerdMigration();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Herd Migration");
    }

    private void castHerdMigration() {
        harness.setHand(player1, List.of(new HerdMigration()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
