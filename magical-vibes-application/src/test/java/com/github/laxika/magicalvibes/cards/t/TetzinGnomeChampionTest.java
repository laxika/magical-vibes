package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DireBlunderbuss;
import com.github.laxika.magicalvibes.cards.d.DireFlail;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.TheGoldenGearColossus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TetzinGnomeChampion.class, TheGoldenGearColossus.class, DireFlail.class,
        DireBlunderbuss.class, DarksteelRelic.class, Forest.class, Plains.class})
class TetzinGnomeChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Tetzin mills three cards and may return a milled artifact to hand")
    void etbMillsAndReturnsArtifact() {
        Card artifact = new DarksteelRelic();
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), artifact));

        harness.enterBattlefieldAndReturn(player1, new TetzinGnomeChampion());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Darksteel Relic");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Crafting Tetzin with six artifacts returns it transformed")
    void craftReturnsTransformed() {
        Permanent tetzin = addReady(player1, new TetzinGnomeChampion());
        for (int i = 0; i < 6; i++) {
            addReady(player1, new DarksteelRelic());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tetzin);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof TheGoldenGearColossus);
    }

    @Test
    @DisplayName("The Golden-Gear Colossus transforms another double-faced artifact and creates Gnomes when it attacks")
    void backFaceAttackTransformsOtherDoubleFacedArtifactAndCreatesGnomes() {
        Permanent colossus = addTransformedColossus();
        Permanent doubleFacedArtifact = addReady(player1, new DireFlail());
        Permanent ordinaryArtifact = addReady(player1, new DarksteelRelic());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(doubleFacedArtifact.getId()).doesNotContain(ordinaryArtifact.getId());
        harness.handlePermanentChosen(player1, doubleFacedArtifact.getId());
        harness.passBothPriorities();

        assertThat(doubleFacedArtifact.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                p.getCard().hasType(CardType.ARTIFACT) && p.getCard().getSubtypes().contains(CardSubtype.GNOME))
                .hasSize(2);
        assertThat(colossus.isTransformed()).isTrue();
    }

    private Permanent addTransformedColossus() {
        TetzinGnomeChampion front = new TetzinGnomeChampion();
        Permanent colossus = new Permanent(front);
        colossus.setCard(front.getBackFaceCard());
        colossus.setTransformed(true);
        colossus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(colossus);
        return colossus;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
