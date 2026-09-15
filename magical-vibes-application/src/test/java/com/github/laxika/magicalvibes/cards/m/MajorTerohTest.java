package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.cards.t.TaintedField;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MajorTeroh.class, NantukoShade.class, TerohsFaithful.class, BaskingRootwalla.class, TaintedField.class})
class MajorTerohTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Major Teroh exiles all black creatures")
    void exilesAllBlackCreatures() {
        Permanent majorTeroh = addCreatureReady(player1, new MajorTeroh());
        Permanent ownBlackCreature = addCreatureReady(player1, new NantukoShade());
        Permanent opposingBlackCreature = addCreatureReady(player2, new NantukoShade());
        Permanent opposingGreenCreature = addCreatureReady(player2, new BaskingRootwalla());
        Permanent opposingWhiteCreature = addCreatureReady(player2, new TerohsFaithful());
        Permanent noncreaturePermanent = harness.addToBattlefieldAndReturn(player2, new TaintedField());

        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(majorTeroh, ownBlackCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBlackCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opposingGreenCreature, opposingWhiteCreature, noncreaturePermanent);
    }

    @Test
    @DisplayName("Only black creatures on the battlefield are exiled")
    void exilesBlackCreaturesOnlyFromBattlefields() {
        Permanent majorTeroh = addCreatureReady(player1, new MajorTeroh());
        Permanent battlefieldBlackCreature = addCreatureReady(player2, new NantukoShade());
        var handBlackCreature = new NantukoShade();
        var graveyardBlackCreature = new NantukoShade();
        harness.setHand(player2, List.of(handBlackCreature));
        gd.playerGraveyards.get(player2.getId()).add(graveyardBlackCreature);

        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(battlefieldBlackCreature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handBlackCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardBlackCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(majorTeroh);
    }

    @Test
    @DisplayName("Sacrifice is paid before Major Teroh's ability resolves")
    void sacrificeIsPaidOnActivation() {
        Permanent majorTeroh = addCreatureReady(player1, new MajorTeroh());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(majorTeroh);
        harness.assertInGraveyard(player1, "Major Teroh");
        assertThat(gd.stack).hasSize(1);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
