package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionExtruder.class, GrizzlyBears.class, Spellbook.class})
class LegionExtruderTest extends BaseCardTest {

    @Test
    void entersAndDealsTwoDamageToAnyTargetPlayer() {
        harness.setHand(player1, List.of(new LegionExtruder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void entersAndDealsTwoDamageToAnyTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LegionExtruder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sacrificesAnotherArtifactAndCreatesGolemToken() {
        harness.addToBattlefield(player1, new LegionExtruder());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent extruder = findPermanent(player1, "Legion Extruder");
        extruder.setSummoningSick(false);
        int extruderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(extruder);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, extruderIndex, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Golem")
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    void cannotSacrificeLegionExtruderItself() {
        harness.addToBattlefield(player1, new LegionExtruder());
        Permanent extruder = findPermanent(player1, "Legion Extruder");
        extruder.setSummoningSick(false);
        int extruderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(extruder);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, extruderIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");
    }
}
