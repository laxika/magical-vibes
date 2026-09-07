package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "226")
public class GlissaHeraldOfPredation extends Card {

    public GlissaHeraldOfPredation() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Incubate 2 twice",
                        List.of(incubatorToken(2), incubatorToken(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Transform all Incubator tokens you control",
                        new TransformAllEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentNamedPredicate("Incubator"),
                                new PermanentControlledBySourceControllerPredicate())))),
                new ChooseOneEffect.ChooseOneOption(
                        "Phyrexians you control gain first strike and deathtouch until end of turn",
                        new GrantKeywordEffect(
                                Set.of(Keyword.FIRST_STRIKE, Keyword.DEATHTOUCH),
                                GrantScope.OWN_PERMANENTS,
                                new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN)))
        )));
    }

    private static CreateTokenEffect incubatorToken(int counters) {
        ActivatedAbility transform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(transform),
                false, false, false, counters, Set.of()
        );
    }
}
