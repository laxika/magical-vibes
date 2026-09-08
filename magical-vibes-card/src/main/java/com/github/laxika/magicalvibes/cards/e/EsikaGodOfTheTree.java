package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.ThePrismaticBridge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "168")
public class EsikaGodOfTheTree extends Card {

    public EsikaGodOfTheTree() {
        setBackFaceCard(new ThePrismaticBridge());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapForAnyColor());

        var legendaryCreature = new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES, legendaryCreature));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(), GrantScope.OWN_CREATURES, legendaryCreature));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Esika, God of the Tree", List.of())
                        .withManaCost("{1}{G}{G}"),
                new ChooseOneEffect.ChooseOneOption("The Prismatic Bridge", List.of())
                        .withManaCost("{W}{U}{B}{R}{G}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThePrismaticBridge";
    }
}
