package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "224")
public class RadhaHeartOfKeld extends Card {

    public RadhaHeartOfKeld() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());

        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}{G}",
                List.of(new BoostSelfEffect(landsYouControl, landsYouControl)),
                "{4}{R}{G}: Radha, Heart of Keld gets +X/+X until end of turn, where X is the number of lands you control."
        ));
    }
}
