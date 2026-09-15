package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzaLordHighArtificer.class, CopperMyr.class, GrizzlyBears.class})
class UrzaLordHighArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Construct that scales with artifacts you control")
    void constructScalesWithControlledArtifacts() {
        Permanent firstArtifact = addCreatureReady(player1, new CopperMyr());
        harness.setHand(player1, java.util.List.of(new UrzaLordHighArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        harness.addToBattlefield(player1, new CopperMyr());
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping an artifact with the first ability adds blue mana")
    void tapsArtifactForBlueMana() {
        Permanent urza = addCreatureReady(player1, new UrzaLordHighArtificer());
        Permanent artifact = addCreatureReady(player1, new CopperMyr());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(urza), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability exiles the shuffled top card for free play")
    void exilesTopCardForFreePlay() {
        Permanent urza = addCreatureReady(player1, new UrzaLordHighArtificer());
        Card top = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(urza), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());

        gd.playerManaPools.get(player1.getId()).clear();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(SpellCastingService.class)
                .playCardFromExile(gd, player1, top.getId(), 0, null));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
