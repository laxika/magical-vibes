package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpsMischief.class, Boomerang.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class ImpsMischiefTest extends BaseCardTest {

    @Test
    void retargetsSingleTargetSpellAndLosesLifeEqualToManaValue() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears2PermId)
                .doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears1PermId));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears2PermId));
    }

    @Test
    void stillLosesLifeWhenThereIsNoLegalNewTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bearsPermId);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bearsPermId));
    }

    @Test
    void castingRequiresTargetingSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }
}
