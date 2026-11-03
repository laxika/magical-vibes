package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "116")
@CardRegistration(set = "WHO", collectorNumber = "721")
public class Blink extends Card {

    public Blink() {
        ShuffleTargetPermanentIntoLibraryEffect shuffleAndInvestigate =
                new ShuffleTargetPermanentIntoLibraryEffect(
                        CreateTokenEffect.ofClueToken(1), ThenEffectRecipient.TARGET_OWNER);
        addTrigger(Triggers.chapter(1), shuffleAndInvestigate);
        addTrigger(Triggers.chapter(3), shuffleAndInvestigate);
        setSagaChapterTargetFilter(1, Set.of(TargetFilters.creature()));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creature()));

        CreateTokenEffect alienAngel = new CreateTokenEffect(
                1,
                "Alien Angel",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ALIEN, CardSubtype.ANGEL),
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                Set.of(CardType.ARTIFACT),
                List.of(
                        new TriggeredAbility(Triggers.opponentCastsSpell()
                                        .withFilter(new CardTypePredicate(CardType.CREATURE)),
                                new RemoveCardTypeFromTargetPermanentEffect(CardType.CREATURE, GrantScope.SELF))));
        addTrigger(Triggers.chapter(2), alienAngel);
        addTrigger(Triggers.chapter(4), alienAngel);
    }
}
