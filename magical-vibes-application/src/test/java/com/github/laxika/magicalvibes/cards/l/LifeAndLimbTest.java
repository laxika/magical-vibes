package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SaprolingMigration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeAndLimb.class, Forest.class, Mountain.class, SaprolingMigration.class})
class LifeAndLimbTest extends BaseCardTest {

    @Test
    @DisplayName("Forests and Saprolings become green 1/1 creature lands")
    void animatesForestsAndSaprolings() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new LifeAndLimb());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.SAPROLING);

        Permanent opponentForest = findPermanent(player2, "Forest");
        assertThat(gqs.isCreature(gd, opponentForest)).isTrue();
        assertThat(gqs.isLand(gd, opponentForest)).isTrue();

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
    }

    @Test
    @DisplayName("Saprolings gain the Forest mana ability")
    void saprolingsTapForGreenMana() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        saproling.setSummoningSick(false);
        harness.addToBattlefield(player1, new LifeAndLimb());

        assertThat(gqs.isCreature(gd, saproling)).isTrue();
        assertThat(gqs.isLand(gd, saproling)).isTrue();
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saproling),
                0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
