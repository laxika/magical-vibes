package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.d.DarkProphecy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfaneMemento;
import com.github.laxika.magicalvibes.cards.r.RiteOfReplication;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeysaKarlov.class, DarkProphecy.class, DoomedTraveler.class, GrizzlyBears.class,
        Shock.class, SoulWarden.class, com.github.laxika.magicalvibes.cards.w.WrathOfGod.class,
        Thragtusk.class, ProfaneMemento.class, RiteOfReplication.class})
class TeysaKarlovTest extends BaseCardTest {

    @Test
    void tokenCopyOfTeysaGrantsItselfVigilanceAndLifelink() {
        harness.addToBattlefield(player2, new TeysaKarlov());
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Teysa Karlov"));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Teysa Karlov");
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doublesLeavesBattlefieldTriggerWhenCreatureDies() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new Thragtusk());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.w.WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
    }

    @Test
    void doublesFromAnywhereTriggerCausedByCreatureDying() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotDoubleOpponentsDeathTriggersOrGrantOpponentsTokensKeywords() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player2, new DoomedTraveler());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Doomed Traveler"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Spirit")).singleElement().satisfies(spirit -> {
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.VIGILANCE)).isFalse();
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isFalse();
        });
        Permanent teysa = findPermanent(player1, "Teysa Karlov");
        assertThat(gqs.hasKeyword(gd, teysa, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, teysa, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void tokensLoseGrantedKeywordsWhenTeysaDies() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Doomed Traveler"));
        harness.passBothPriorities();
        resolveAllTriggers();
        UUID teysaId = harness.getPermanentId(player1, "Teysa Karlov");
        harness.castInstant(player2, 0, teysaId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, teysaId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Teysa Karlov")).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(spirit -> {
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.VIGILANCE)).isFalse();
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isFalse();
        });
    }

    @Test
    void doublesTriggeredAbilitiesCausedByCreatureDeath() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void doublesDeathTriggersEvenWhenTeysaDiesAtTheSameTime() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.w.WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    void grantsVigilanceAndLifelinkToCreatureTokens() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID travelerId = harness.getPermanentId(player1, "Doomed Traveler");
        harness.castInstant(player2, 0, travelerId);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    void doesNotDoubleUnrelatedTriggeredAbilities() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
