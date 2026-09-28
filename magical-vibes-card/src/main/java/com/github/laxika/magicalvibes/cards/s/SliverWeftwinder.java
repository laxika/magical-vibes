package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BelligerentSliver;
import com.github.laxika.magicalvibes.cards.b.BladebackSliver;
import com.github.laxika.magicalvibes.cards.b.BlurSliver;
import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.c.CleavingSliver;
import com.github.laxika.magicalvibes.cards.c.CloudshredderSliver;
import com.github.laxika.magicalvibes.cards.d.DiffusionSliver;
import com.github.laxika.magicalvibes.cards.d.DregscapeSliver;
import com.github.laxika.magicalvibes.cards.e.EnduringSliver;
import com.github.laxika.magicalvibes.cards.f.FirstSliversChosen;
import com.github.laxika.magicalvibes.cards.h.HollowheadSliver;
import com.github.laxika.magicalvibes.cards.l.LancerSliver;
import com.github.laxika.magicalvibes.cards.l.LavabellySliver;
import com.github.laxika.magicalvibes.cards.l.LeechingSliver;
import com.github.laxika.magicalvibes.cards.m.ManaweftSliver;
import com.github.laxika.magicalvibes.cards.p.PredatorySliver;
import com.github.laxika.magicalvibes.cards.s.ScuttlingSliver;
import com.github.laxika.magicalvibes.cards.s.SentinelSliver;
import com.github.laxika.magicalvibes.cards.s.SliverHivelord;
import com.github.laxika.magicalvibes.cards.s.SpitefulSliver;
import com.github.laxika.magicalvibes.cards.s.SteelformSliver;
import com.github.laxika.magicalvibes.cards.s.StrikingSliver;
import com.github.laxika.magicalvibes.cards.t.TemperedSliver;
import com.github.laxika.magicalvibes.cards.t.TheFirstSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@CardRegistration(set = "YEOE", collectorNumber = "25")
public class SliverWeftwinder extends Card {

    private static final List<Supplier<? extends Card>> SLIVERS_SPELLBOOK = List.of(
            BelligerentSliver::new,
            BladebackSliver::new,
            BlurSliver::new,
            BonescytheSliver::new,
            CleavingSliver::new,
            CloudshredderSliver::new,
            DiffusionSliver::new,
            DregscapeSliver::new,
            EnduringSliver::new,
            FirstSliversChosen::new,
            HollowheadSliver::new,
            LancerSliver::new,
            LavabellySliver::new,
            LeechingSliver::new,
            ManaweftSliver::new,
            PredatorySliver::new,
            ScuttlingSliver::new,
            SentinelSliver::new,
            SliverHivelord::new,
            SpitefulSliver::new,
            SteelformSliver::new,
            StrikingSliver::new,
            TemperedSliver::new,
            TheFirstSliver::new);

    public SliverWeftwinder() {
        CardSubtypePredicate sliverCard = new CardSubtypePredicate(CardSubtype.SLIVER);
        PermanentHasSubtypePredicate sliverCreature = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, AlternativeCostForSpellsEffect.warp("{3}", sliverCard));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(randomSliverConjure(), new DrawCardEffect(1)),
                GrantScope.ALL_OWN_CREATURES,
                sliverCreature));
    }

    private static ConjureCardIntoTopCardsOfLibraryEffect randomSliverConjure() {
        return new ConjureCardIntoTopCardsOfLibraryEffect(
                () -> SLIVERS_SPELLBOOK.get(ThreadLocalRandom.current().nextInt(SLIVERS_SPELLBOOK.size())).get(),
                5);
    }
}
