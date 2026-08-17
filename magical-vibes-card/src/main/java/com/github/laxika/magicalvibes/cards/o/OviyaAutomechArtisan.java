package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandToBattlefieldWithArtifactCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "173")
public class OviyaAutomechArtisan extends Card {

    public OviyaAutomechArtisan() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentIsAttackingOpponentOfSourceControllerPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new MayEffect(
                        new PutCardFromHandToBattlefieldWithArtifactCountersEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.VEHICLE))),
                                "creature or Vehicle", 2),
                        "Put a creature or Vehicle card from your hand onto the battlefield?"
                )),
                "{G}, {T}: You may put a creature or Vehicle card from your hand onto the battlefield. "
                        + "If you put an artifact onto the battlefield this way, put two +1/+1 counters on it."
        ));
    }
}
