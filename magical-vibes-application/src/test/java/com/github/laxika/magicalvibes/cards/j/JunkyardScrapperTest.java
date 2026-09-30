package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkyardScrapper.class, FountainOfYouth.class, DarksteelCitadel.class,
        CommandersSphere.class, LlanowarElves.class})
class JunkyardScrapperTest extends BaseCardTest {

    @Test
    void seeksRandomEligibleArtifactAndGrantsPermissionToCastIt() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card sought = new FountainOfYouth();
        harness.setLibrary(player1, List.of(sought, new DarksteelCitadel(), new LlanowarElves()));
        harness.setHand(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(sought);
        assertThat(gd.exilePlayPermissions).containsEntry(sought.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Darksteel Citadel", "Llanowar Elves");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, sought.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void doesNotSeekArtifactsWithManaValueEqualToOrGreaterThanTheSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card tooExpensive = new CommandersSphere();
        Card artifactLand = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(tooExpensive, artifactLand));
        harness.setHand(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive, artifactLand);
    }
}
